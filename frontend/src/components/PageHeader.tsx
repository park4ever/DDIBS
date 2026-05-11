interface PageHeaderProps {
    title: string;
    description?: string;
}

export default function PageHeader({
                                       title,
                                       description,
                                   }: PageHeaderProps) {
    return (
        <div className="page-header">
            <h2>{title}</h2>
            {description ? <p>{description}</p> : null}
        </div>
    );
}